package ru.practicum.android.diploma.ui.vacancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.data.network.RetrofitNetworkClient.Companion.NOT_FOUND_CODE
import ru.practicum.android.diploma.domain.state.VacancyDetailsState
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Data.Empty
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Data.Error
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Data.Loading
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Data.Payload
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Favorite.InFavorite
import ru.practicum.android.diploma.domain.state.VacancyDetailsState.Favorite.NotInFavorite
import ru.practicum.android.diploma.domain.usecase.GetVacancyDetailsUseCase
import ru.practicum.android.diploma.domain.usecase.favorite.AddVacancyToFavoriteUseCase
import ru.practicum.android.diploma.domain.usecase.favorite.DeleteVacancyFromFavoriteUseCase
import ru.practicum.android.diploma.domain.usecase.favorite.GetFavoriteVacancyByIdUseCase

class VacancyViewModel(
    private val getVacancyDetailsUseCase: GetVacancyDetailsUseCase,
    private val getFavoriteVacancyByIdUseCase: GetFavoriteVacancyByIdUseCase,
    private val addVacancyToFavoriteUseCase: AddVacancyToFavoriteUseCase,
    private val deleteVacancyFromFavoriteUseCase: DeleteVacancyFromFavoriteUseCase
) : ViewModel() {

    private val _state = MutableLiveData<VacancyDetailsState>()
    val state: LiveData<VacancyDetailsState> get() = _state

    init {
        _state.postValue(VacancyDetailsState(Loading, NotInFavorite))
    }

    fun getVacancyDetails(id: String) = viewModelScope.launch {
        val details = getVacancyDetailsUseCase.execute(id)
        val detailsState = when (details.first) {
            null -> if (details.second == NOT_FOUND_CODE.toString()) Empty else Error
            else -> Payload(details.first!!)
        }

        val favorite = getFavoriteVacancyByIdUseCase.execute(id)
        val favoriteState = if (favorite == null) NotInFavorite else InFavorite

        _state.postValue(VacancyDetailsState(detailsState, favoriteState))
    }

    @Suppress("LabeledExpression")
    fun onFavoriteClicked(vacancyId: String) = viewModelScope.launch {
        when (val dataState = _state.value?.data) {
            is Payload -> {
                val dbFavoriteVacancy = getFavoriteVacancyByIdUseCase.execute(vacancyId)
                val favoriteState = if (dbFavoriteVacancy == null) {
                    addVacancyToFavoriteUseCase.execute(dataState.details)
                    InFavorite
                } else {
                    deleteVacancyFromFavoriteUseCase.execute(dataState.details)
                    NotInFavorite
                }

                _state.postValue(VacancyDetailsState(dataState, favoriteState))
            }

            else -> return@launch
        }
    }
}
