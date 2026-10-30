package es.killobyte.nucleo.api.model

import java.time.Month

enum class Season {
    WINTER, SPRING, SUMMER, AUTUMN;

    companion object {
        fun of(month: Month): Season = when (month) {
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> WINTER
            Month.MARCH, Month.APRIL, Month.MAY -> SPRING
            Month.JUNE, Month.JULY, Month.AUGUST -> SUMMER
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> AUTUMN
        }
    }
}
