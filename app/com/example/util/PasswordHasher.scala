package com.example.util

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
  def hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
  def check(password: String, hash: String) = BCrypt.checkpw(password, hash)
}