package com.javic.pokewhere.util;

/**
 * Created by victor on 28/10/16.
 */

import com.javic.pokewhere.models.LocalUserPokemon;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Calendar;
import java.util.Comparator;

/**
 * This comparator sorts a list of Creation time by year, month, day, minute, second
 * into ascending order.
 */
public class PokemonCreationTimeComparator implements Comparator<LocalUserPokemon> {

    @Override
    public int compare(LocalUserPokemon p1, LocalUserPokemon p2) {
        return new CompareToBuilder()
                .append(p2.getCreationTime().get(Calendar.YEAR), p1.getCreationTime().get(Calendar.YEAR))
                .append(p2.getCreationTime().get(Calendar.MONTH), p1.getCreationTime().get(Calendar.MONTH))
                .append(p2.getCreationTime().get(Calendar.DAY_OF_MONTH), p1.getCreationTime().get(Calendar.DAY_OF_MONTH))
                .append(p2.getCreationTime().get(Calendar.HOUR), p1.getCreationTime().get(Calendar.HOUR))
                .append(p2.getCreationTime().get(Calendar.MINUTE), p1.getCreationTime().get(Calendar.MINUTE))
                .toComparison();
    }
}
