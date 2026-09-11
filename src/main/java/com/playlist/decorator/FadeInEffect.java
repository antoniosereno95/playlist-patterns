package com.playlist.decorator;

/**
 * Efeito que aplica uma rampa linear de volume nas primeiras amostras.
 */
public final class FadeInEffect extends AudioEffect {

  private final int sampleCount;

  /**
   * Cria o efeito de fade in.
   *
   * @param wrapped áudio decorado.
   * @param sampleCount quantidade de amostras usadas na rampa.
   */
  public FadeInEffect(AudioTrack wrapped, int sampleCount) {
    super(wrapped);
    this.sampleCount = sampleCount;
  }

  @Override
  protected String describe() {
    return "fadeIn(" + this.sampleCount + ")";
  }

  @Override
  public double[] getSamples() {
    double[] samples = this.wrapped.getSamples();

    if (this.sampleCount <= 0) {
      return samples;
    }

    int limit = Math.min(this.sampleCount, samples.length);
    for (int i = 0; i < limit; i++) {
      samples[i] *= (double) i / this.sampleCount;
    }

    return samples;
  }
}