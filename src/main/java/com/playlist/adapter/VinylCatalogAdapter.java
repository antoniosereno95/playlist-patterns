package com.playlist.adapter;

import com.playlist.adapter.external.LegacyVinylCatalog;
import com.playlist.core.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Adapter que converte os registros do {@link LegacyVinylCatalog} para {@link Track}.
 */
public class VinylCatalogAdapter implements TrackCatalog {

  private final LegacyVinylCatalog legacyCatalog;

  /**
   * Cria o adapter em cima do sistema legado.
   *
   * @param legacyCatalog catálogo legado a ser adaptado. Não pode ser nulo.
   * @throws IllegalArgumentException se o catálogo for nulo.
   */
  public VinylCatalogAdapter(LegacyVinylCatalog legacyCatalog) {
    if (legacyCatalog == null) {
      throw new IllegalArgumentException("O catálogo legado não pode ser nulo.");
    }
    this.legacyCatalog = legacyCatalog;
  }

  @Override
  public List<Track> findAll() {
    String[] records = legacyCatalog.fetchAllRecords();
    if (records == null) {
      return List.of();
    }

    List<Track> tracks = new ArrayList<>();
    for (String row : records) {
      parse(row).ifPresent(tracks::add);
    }
    return tracks;
  }

  @Override
  public Optional<Track> findById(String id) {
    if (id == null || id.isBlank()) {
      return Optional.empty();
    }

    String record = legacyCatalog.findRecordByCatalogNumber(id);
    if (record == null) {
      return Optional.empty();
    }

    return parse(record);
  }

  /**
   * Método auxiliar para converter uma linha de texto do sistema legado em um objeto {@link Track}.
   *
   * @param row linha no formato "catalogNumber|title|artist|durationMs|premium"
   * @return {@link Optional} contendo o {@link Track} se a linha for válida, ou {@link Optional#empty()} caso contrário.
   */
  private Optional<Track> parse(String row) {
    if (row == null || row.isBlank()) {
      return Optional.empty();
    }

    // Divide a linha pelo separador '|', mantendo campos vazios (-1)
    String[] parts = row.split("\\|", -1);
    if (parts.length != 5) {
      return Optional.empty();
    }

    String catalogNumber = parts[0].trim();
    String rawTitle = parts[1];
    String rawArtist = parts[2];
    String rawDurationMs = parts[3];
    String rawPremium = parts[4];

    // 1. Validar ID
    if (catalogNumber.isEmpty()) {
      return Optional.empty();
    }

    // 2. Format e validar Título
    String formattedTitle = formatWords(rawTitle.trim().replaceAll("\\s+", " "));
    if (formattedTitle.isEmpty()) {
      return Optional.empty();
    }

    // 3. Format e validar Artista (Formato esperado: "SOBRENOME, NOME")
    String[] artistParts = rawArtist.trim().split(",", 2);
    if (artistParts.length != 2) {
      return Optional.empty();
    }

    String surname = artistParts[0].trim();
    String name = artistParts[1].trim();

    if (surname.isEmpty() || name.isEmpty()) {
      return Optional.empty();
    }

    String formattedSurname = formatWords(surname.replaceAll("\\s+", " "));
    String formattedName = formatWords(name.replaceAll("\\s+", " "));
    String formattedArtist = formattedName + " " + formattedSurname;

    // 4. Converter e validar Duração (Ms -> Segundos)
    int durationSeconds;
    try {
      long durationMs = Long.parseLong(rawDurationMs.trim());
      if (durationMs < 0) {
        return Optional.empty();
      }
      durationSeconds = (int) (durationMs / 1000);
    } catch (NumberFormatException e) {
      return Optional.empty();
    }

    // 5. Validar Flag Premium
    boolean premium = rawPremium.trim().equalsIgnoreCase("Y");

    return Optional.of(new Track(catalogNumber, formattedTitle, formattedArtist, durationSeconds, premium));
  }

  /**
   * Auxiliar para capitalizar a primeira letra de cada palavra e deixar as demais minúsculas.
   */
  private String formatWords(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }

    return Arrays.stream(text.split(" "))
        .filter(word -> !word.isEmpty())
        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
        .reduce((w1, w2) -> w1 + " " + w2)
        .orElse("");
  }
}