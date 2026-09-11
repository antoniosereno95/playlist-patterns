package com.playlist.decorator;

import java.util.Locale;

/**
 * Efeito que multiplica o volume das amostras, com corte em {@code [-1.0, 1.0]}.
 */
public final class VolumeEffect extends AudioEffect {

  private final double factor;

  /**
   * Cria o efeito de volume.
   *
   * @param wrapped áudio decorado.
   * @param factor fator multiplicador do volume.
   */
  public VolumeEffect(AudioTrack wrapped, double factor) {
    super(wrapped);
    this.factor = factor;
  }

  @Override
  protected String describe() {
    return String.format(Locale.ROOT, "volume(%.1f)", this.factor);
  }

  @Override
  public double[] getSamples() {
    double[] samples = this.wrapped.getSamples();

    for (int i = 0; i < samples.length; i++) {
      double newSample = samples[i] * this.factor;

      // Aplica o "clamp" / corte no intervalo [-1.0, 1.0]
      if (newSample > 1.0) {
        newSample = 1.0;
      } else if (newSample < -1.0) {
        newSample = -1.0;
      }

      samples[i] = newSample;
    }

    return samples;
  }
}