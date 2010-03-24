package com.thinkminimo.step

import scala.actors.Actor
import scala.actors.TIMEOUT
import scala.xml.Text
import org.scalatest.matchers.ShouldMatchers

class StepTestServlet extends Step {
  get("/") {
    "root"
  }

  get("/this/:test/should/:pass") {
    params(":test")+params(":pass")
  }

  get("/xml/:must/:val") {
    <h1>{ params(":must")+params(":val") }</h1>
  }

  get("/number") {
    42
  }

  post("/post/test") {
    params.get("posted_value") match {
      case None => "posted_value is null"
      case Some(s) => s
    }
  }

  post("/post/:test/val") {
    params("posted_value")+params(":test")
  }

  get("/no_content") {
    status(204)
  }

  get("/redirect") {
    redirect("/redirected")
  }

  get("/redirected") {
    "redirected"
  }

  get("/print_referer") {
    (request referer) getOrElse ("NONE")
  }

  get("/binary/test") {
    "test".getBytes
  }

  get("/returns-unit") {
    ()
  }

  get("/halts-response") {
    response.setHeader("testHeader", "testHeader")
    halt(501, "Not implemented (for test)")
    "this content must not be returned"
  }
}

class StepTest extends StepSuite with ShouldMatchers {
  route(classOf[StepTestServlet], "/*")

  test("GET / should return 'root'") {
    get("/") {
      body should equal ("root")
    }
  }

  test("GET /this/will/should/work should return 'willwork'") {
    get("/this/will/should/work") {
      body should equal ("willwork")
    }
  }

  test("GET /xml/really/works should return '<h1>reallyworks</h1>'") {
    get("/xml/really/works") {
      body should equal ("<h1>reallyworks</h1>")
    }
  }

  test("GET /number should return '42'") {
    get("/number") {
      body should equal ("42")
    }
  }

  test("POST /post/test with posted_value=yes should return 'yes'") {
    post("/post/test", "posted_value" -> "yes") {
      body should equal ("yes")
    }
  }

  test("POST /post/something/val with posted_value=yes should return 'yessomething'") {
    post("/post/something/val", "posted_value" -> "yes") {
      body should equal ("yessomething")
    }
  }

  test("GET /no_content should return 204(HttpServletResponse.SC_NO_CONTENT)") {
    get("/no_content") {
      status should equal (204)
      body should equal ("")
    }
  }

  test("GET /redirect redirects to /redirected") {
    get("/redirect") {
      header("Location") should endWith ("/redirected")
    }
  }

  test("POST /post/test with posted_value=<multi-byte str> should return the multi-byte str") {
    post("/post/test", "posted_value" -> "こんにちは") {
      body should equal ("こんにちは")
    }
  }

  test("GET /print_referer should return Referer") {
    get("/print_referer", Map.empty[String, String], Map("Referer" -> "somewhere")) {
      body should equal ("somewhere")
    }
  }

  test("GET /print_referer should return NONE when no referer") {
    get("/print_referer") {
      body should equal ("NONE")
    }
  }

  test("POST /post/test without params return \"posted_value is null\"") {
    post("/post/test") {
      body should equal ("posted_value is null")
    }
  }

  test("render binary response when action returns a byte array") {
    get("/binary/test") {
      body should equal("test")
    }
  }

  test("Do not output response body if action returns Unit") {
    get("/returns-unit") {
      body should equal ("")
    }
  }

  test("GET /halts-response halts processing of the action") {
    get("/halts-response") {
      status should equal(501)
      body should not equal("this content must not be returned")
      body.contains("Not implemented (for test)")
    }
  }

  test("GET /halts-response - halt doesn't clear headers") {
    get("/halts-response") {
      response.getHeader("testHeader") should equal("testHeader")
    }
  }

}
