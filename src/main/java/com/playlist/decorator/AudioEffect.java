package com.playlist.decorator;

/**
 * Decorator abstrato: envolve outro {@link AudioTrack} e acrescenta um efeito.
 */
public abstract class AudioEffect implements AudioTrack {

  /**
   * O áudio decorado por este efeito.
   */
  protected final AudioTrack wrapped;

  /**
   * Guarda o áudio que será decorado.
   *
   * @param wrapped áudio decorado. Não pode ser nulo.
   * @throws IllegalArgumentException se {@code wrapped} for nulo.
   */
  protected AudioEffect(AudioTrack wrapped) {
    if (wrapped == null) {
      throw new IllegalArgumentException("O áudio decorado (wrapped) não pode ser nulo.");
    }
    this.wrapped = wrapped;
  }

  /**
   * Nome do efeito, já formatado, usado na cadeia de efeitos.
   *
   * @return por exemplo {@code "volume(2.0)"}.
   */
  protected abstract String describe();

  @Override
  public String getTitle() {
    return this.wrapped.getTitle();
  }

  @Override
  public String getEffectChain() {
    return this.wrapped.getEffectChain() + " -> " + describe();
  }
}