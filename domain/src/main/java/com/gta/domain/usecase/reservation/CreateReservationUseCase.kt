package com.gta.domain.usecase.reservation

import com.gta.domain.model.Notification
import com.gta.domain.model.NotificationType
import com.gta.domain.model.Reservation
import com.gta.domain.repository.ReservationRepository
import com.gta.domain.usecase.SendNotificationUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateReservationUseCase @Inject constructor(
    private val repository: ReservationRepository,
    private val notificationUseCase: SendNotificationUseCase
) {
    suspend operator fun invoke(reservation: Reservation, ownerId: String): Boolean {
        val reservationId = repository.createReservation(reservation).first()
        val notificationResult = notificationUseCase(
            Notification(
                type = NotificationType.REQUEST_RESERVATION.title,
                message = NotificationType.REQUEST_RESERVATION.msg,
                reservationId = reservationId,
                fromId = reservation.userId
            ),
            ownerId
        )
        return reservationId.isNotEmpty() && notificationResult
    }
}
