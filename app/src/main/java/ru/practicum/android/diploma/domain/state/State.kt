package ru.practicum.android.diploma.domain.state

import androidx.annotation.DrawableRes

internal data class State(
    val input: Input,
    val vacanciesList: VacanciesList
) {

    sealed interface Input {
        data object Empty : Input
        data class Text(val value: String) : Input
    }

    sealed interface VacanciesList {
        data object Empty : VacanciesList
        data object NoInternet : VacanciesList
        data object Loading : VacanciesList
        data object Error : VacanciesList
        data class Data(val vacancies: List<Vacancy>) : VacanciesList
    }
}

internal class Vacancy(
    val title: String,
    @DrawableRes val icon: Int
    // todo etc
)
