package com.example.services

import com.example.dao.DataAccessLayer

trait ServicesLayer
  extends RoomManagerComponent
  with GameActorComponent
  with SeatActorComponent
  with RatingActorComponent {
  this: DataAccessLayer =>
}