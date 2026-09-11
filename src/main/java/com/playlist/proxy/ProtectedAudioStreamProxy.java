package com.playlist.proxy;

import com.playlist.core.AccessDeniedException;
import com.playlist.core.Subscription;
import com.playlist.core.Track;
import java.util.function.Supplier;

/**
 * Proxy que controla o acesso ao {@link RemoteAudioStream}.
 *
 * Ele acumula três responsabilidades clássicas do padrão: proteção
 * (bloqueia faixas premium para o plano gratuito), lazy loading (só cria o
 * objeto real quando o áudio é realmente pedido) e cache (não baixa o mesmo
 * áudio duas vezes).
 */
public class ProtectedAudioStreamProxy implements AudioStream {

  private final Track track;
  private final Subscription plan;
  private final Supplier<AudioStream> loader;
  private AudioStream real;
  private byte[] cachedBytes;

  /**
   * Cria o proxy com uma fábrica explícita do objeto real.
   *
   * @param track faixa que será transmitida.
   * @param plan plano de assinatura de quem está ouvindo.
   * @param loader fábrica que cria o stream real. Só pode ser chamada quando o
   *     áudio for realmente necessário.
   * @throws IllegalArgumentException se qualquer argumento for nulo.
   */
  public ProtectedAudioStreamProxy(Track track, Subscription plan, Supplier<AudioStream> loader) {
    if (track == null || plan == null || loader == null) {
      throw new IllegalArgumentException("Nenhum dos argumentos do construtor pode ser nulo.");
    }
    this.track = track;
    this.plan = plan;
    this.loader = loader;
  }

  /**
   * Cria o proxy usando {@link RemoteAudioStream} como objeto real.
   *
   * @param track faixa que será transmitida.
   * @param plan plano de assinatura de quem está ouvindo.
   */
  public ProtectedAudioStreamProxy(Track track, Subscription plan) {
    this(track, plan, () -> new RemoteAudioStream(track));
  }

  /**
   * Indica se o objeto real já foi criado.
   *
   * @return {@code true} apenas depois que o stream real tiver sido carregado.
   */
  public boolean isLoaded() {
    return this.real != null;
  }

  @Override
  public String getTrackId() {
    return this.track.id();
  }

  /**
   * Devolve os bytes do áudio, respeitando plano, carga preguiçosa e cache.
   *
   * @return uma cópia dos bytes do áudio.
   * @throws AccessDeniedException se a faixa for premium e o plano for
   *     {@link Subscription#FREE}.
   */
  @Override
  public byte[] readBytes() {
    // 1. Proteção: bloqueia acesso antes de acionar o loader
    if (this.track.premium() && this.plan == Subscription.FREE) {
      throw new AccessDeniedException("Assinantes do plano FREE não têm acesso a faixas premium.");
    }

    // 2. Carga Preguiçosa (Lazy Loading) e Cache
    if (this.cachedBytes == null) {
      if (this.real == null) {
        this.real = this.loader.get();
      }
      this.cachedBytes = this.real.readBytes();
    }

    // 3. Cópia defensiva para não expor o array interno
    return this.cachedBytes.clone();
  }
}