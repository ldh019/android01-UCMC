package com.gta.presentation.ui.reservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gta.domain.model.AvailableDate
import com.gta.domain.model.CarRentInfo
import com.gta.domain.model.InsuranceOption
import com.gta.domain.model.Reservation
import com.gta.domain.usecase.reservation.CreateReservationUseCase
import com.gta.domain.usecase.reservation.GetCarRentInfoUseCase
import com.gta.presentation.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val args: SavedStateHandle,
    getCarRentInfoUseCase: GetCarRentInfoUseCase,
    private val createReservationUseCase: CreateReservationUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _reservationDate = MutableLiveData<AvailableDate>()
    val reservationDate: LiveData<AvailableDate> get() = _reservationDate

    private val _insuranceOption = MutableLiveData<InsuranceOption>()
    val insuranceOption: LiveData<InsuranceOption> get() = _insuranceOption

    private val _totalPrice = MediatorLiveData<Int>()
    val totalPrice: LiveData<Int> get() = _totalPrice

    val car: StateFlow<CarRentInfo>? = args.get<String>("carId")?.let { carId ->
        getCarRentInfoUseCase(carId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CarRentInfo()
        )
    }

    private val _createReservationEvent = MutableSharedFlow<Boolean>()
    val createReservationEvent: SharedFlow<Boolean> get() = _createReservationEvent

    val basePrice: LiveData<Int> = Transformations.map(_reservationDate) {
        val carPrice = car?.value?.price ?: 0
        DateUtil.getDateCount(it.start, it.end) * carPrice
    }

    private val _isPaymentOptionChecked = MutableStateFlow(false)
    val isPaymentOptionChecked: StateFlow<Boolean> get() = _isPaymentOptionChecked

    init {
        _totalPrice.addSource(basePrice) { basePrice ->
            val insurancePrice = insuranceOption.value?.price ?: 0
            _totalPrice.value = basePrice.plus(insurancePrice)
        }

        _totalPrice.addSource(insuranceOption) { option ->
            val price = basePrice.value ?: 0
            _totalPrice.value = price.plus(option.price)
        }
    }

    fun setReservationDate(selected: AvailableDate) {
        _reservationDate.value = selected
    }

    fun setInsuranceOption(option: InsuranceOption) {
        _insuranceOption.value = option
    }

    fun setIsPaymentOptionChecked(isChecked: Boolean) {
        _isPaymentOptionChecked.value = isChecked
    }

    fun createReservation() {
        val userId = auth.currentUser?.uid ?: return
        val date = reservationDate.value ?: return
        val price = totalPrice.value ?: return
        val option = insuranceOption.value ?: return

        args.get<String>("carId")?.let {
            viewModelScope.launch {
                _createReservationEvent.emit(
                    createReservationUseCase(
                        Reservation(
                            carId = it,
                            userId = userId,
                            reservationDate = date,
                            price = price,
                            insuranceOption = option.name
                        )
                    ).first()
                )
            }
        }
    }
}
