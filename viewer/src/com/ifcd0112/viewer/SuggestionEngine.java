package com.ifcd0112.viewer;

import java.util.List;

/** Motor de sugerencias para el popup del editor. */
@FunctionalInterface
public interface SuggestionEngine {

    int MAX_RESULTS = 40;
    int MIN_PREFIX_AUTO = 1;

    List<String> suggest(String prefix);
}
