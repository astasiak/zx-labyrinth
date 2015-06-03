package actors

import org.junit.Test
import org.junit.Assert._
import org.junit.Before

class TestX {
  
  var hello: String = _
  
  @Before def setUp {
    hello = "Hello World!"
  }
 
  @Test def someTest {
    assertEquals(hello, "Hello World!")
  }
 
}