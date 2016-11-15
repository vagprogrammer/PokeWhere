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
public class PokemonComparator implements Comparator<LocalUserPokemon> {

    private int valueToCompare;

    public PokemonComparator(int valueToCompare) {
        this.valueToCompare = valueToCompare;
    }

    @Override
    public int compare(LocalUserPokemon p1, LocalUserPokemon p2) {

        CompareToBuilder comparator = new CompareToBuilder();

        switch (valueToCompare){
            case Constants.VALUE_IV:
                comparator.append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_CP:
                comparator.append(p2.getCp(), p1.getCp())
                        .append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_RECENTS:
                Calendar calendar1 = createDate(p1.getCreationTimeMillis());
                Calendar calendar2 = createDate(p2.getCreationTimeMillis());

                    comparator.append(calendar2.get(Calendar.YEAR), calendar1.get(Calendar.YEAR))
                    .append(calendar2.get(Calendar.MONTH), calendar1.get(Calendar.MONTH))
                    .append(calendar2.get(Calendar.DAY_OF_MONTH), calendar1.get(Calendar.DAY_OF_MONTH))
                    .append(calendar2.get(Calendar.HOUR), calendar1.get(Calendar.HOUR))
                    .append(calendar2.get(Calendar.MINUTE), calendar1.get(Calendar.MINUTE))
                            .append(calendar2.get(Calendar.SECOND), calendar1.get(Calendar.SECOND));

                break;
            case Constants.VALUE_NAME:
                comparator.append(p1.getName(), p2.getName())
                        .append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_NUMBER:
                comparator.append(p1.getNumber(), p2.getNumber())
                        .append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_ATACK:
                comparator.append(p2.getAttack(), p1.getAttack())
                        .append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_DEFENSE:
                comparator.append(p2.getDefense(), p1.getDefense())
                        .append(p2.getIv(), p1.getIv());
                break;
            case Constants.VALUE_STAMINA:
                comparator.append(p2.getStamina(), p1.getStamina())
                        .append(p2.getIv(), p1.getIv());
                break;

        }

        return comparator.toComparison();
    }

    private Calendar createDate(long timestamp) {

        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;

    }
}
