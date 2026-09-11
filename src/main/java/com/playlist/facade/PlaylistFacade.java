package com.playlist.facade;

import com.playlist.adapter.TrackCatalog;
import com.playlist.composite.PlaylistNode;
import com.playlist.composite.TrackItem;
import com.playlist.core.Subscription;
import com.playlist.core.Track;
import com.playlist.core.TrackNotFoundException;
import com.playlist.decorator.AudioTrack;
import com.playlist.decorator.FadeInEffect;
import com.playlist.decorator.RawAudioTrack;
import com.playlist.decorator.VolumeEffect;
import com.playlist.proxy.ProtectedAudioStreamProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Fachada que esconde do mundo externo a colaboração entre catálogo, playlists,
 * streams protegidos e efeitos de áudio.
 *
 * Quem usa a Playlist precisa conhecer apenas esta classe.
 */
public class PlaylistFacade {

  private final TrackCatalog catalog;
  private final Subscription plan;
  private final Map<String, ProtectedAudioStreamProxy> streams = new HashMap<>();

  /**
   * Monta a fachada.
   *
   * @param catalog catálogo de faixas já adaptado.
   * @param plan plano de assinatura de quem está usando o sistema.
   * @throws IllegalArgumentException se qualquer argumento for nulo.
   */
  public PlaylistFacade(TrackCatalog catalog, Subscription plan) {
    if (catalog == null || plan == null) {
      throw new IllegalArgumentException("O catálogo e o plano não podem ser nulos.");
    }
    this.catalog = catalog;
    this.plan = plan;
  }

  /**
   * Monta uma playlist com todas as faixas do catálogo, na ordem em que o catálogo as devolve.
   *
   * @param name nome da playlist criada.
   * @return a playlist preenchida.
   */
  public PlaylistNode buildLibrary(String name) {
    PlaylistNode playlist = new PlaylistNode(name);
    for (Track track : this.catalog.findAll()) {
      playlist.add(new TrackItem(track));
    }
    return playlist;
  }

  /**
   * Devolve os bytes de áudio de uma faixa, respeitando o plano de assinatura.
   *
   * @param trackId identificador da faixa.
   * @return os bytes do áudio.
   * @throws TrackNotFoundException se a faixa não existir no catálogo.
   */
  public byte[] listen(String trackId) {
    Track track = requireTrack(trackId);
    ProtectedAudioStreamProxy proxy = this.streams.computeIfAbsent(
        trackId, id -> new ProtectedAudioStreamProxy(track, this.plan)
    );
    return proxy.readBytes();
  }

  /**
   * Monta uma prévia da faixa com volume ajustado e fade in.
   *
   * @param trackId identificador da faixa.
   * @param volume fator de volume aplicado primeiro.
   * @param fadeInSamples quantidade de amostras do fade in, aplicado depois.
   * @return o áudio já decorado.
   * @throws TrackNotFoundException se a faixa não existir no catálogo.
   */
  public AudioTrack preview(String trackId, double volume, int fadeInSamples) {
    Track track = requireTrack(trackId);
    byte[] bytes = listen(trackId);

    double[] samples = new double[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      samples[i] = bytes[i] / 128.0;
    }

    AudioTrack raw = new RawAudioTrack(track.title(), samples);
    AudioTrack withVolume = new VolumeEffect(raw, volume);
    return new FadeInEffect(withVolume, fadeInSamples);
  }

  /**
   * Método auxiliar para buscar uma faixa no catálogo e garantir que ela existe.
   */
  private Track requireTrack(String trackId) {
    return this.catalog.findById(trackId)
        .orElseThrow(() -> new TrackNotFoundException("Faixa não encontrada para o ID: " + trackId));
  }
}