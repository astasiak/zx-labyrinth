package com.example.controllers

import com.example.dao.DataAccessLayer
import com.example.services.ServicesLayer

trait ControllersLayer
  extends ZxController
  with RestController
  with AdminController {
  this: DataAccessLayer with ServicesLayer =>

}