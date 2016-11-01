package com.javic.pokewhere.util;

/**
 * Created by victor on 28/10/16.
 */

import com.javic.pokewhere.models.LocalUserPokemon;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * This comparator sorts a list of Creation time by year, month, day, minute, second
 * into ascending order.
 */
public class PokemonCreationTimeComparator implements Comparator<LocalUserPokemon> {

    @Override
    public int compare(LocalUserPokemon p1, LocalUserPokemon p2) {
        Calendar calendar1 = createDate(p1.getCreationTimeMillis());
        Calendar calendar2 = createDate(p2.getCreationTimeMillis());

        return new CompareToBuilder()
                .append(calendar2.get(Calendar.YEAR), calendar1.get(Calendar.YEAR))
                .append(calendar2.get(Calendar.MONTH), calendar1.get(Calendar.MONTH))
                .append(calendar2.get(Calendar.DAY_OF_MONTH), calendar1.get(Calendar.DAY_OF_MONTH))
                .append(calendar2.get(Calendar.HOUR), calendar1.get(Calendar.HOUR))
                .append(calendar2.get(Calendar.MINUTE), calendar1.get(Calendar.MINUTE))
                .toComparison();
    }

    public Calendar createDate(long timestamp) {

        //Fri Aug 26 19:54:06 CDT 2016
        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;

    }
}
