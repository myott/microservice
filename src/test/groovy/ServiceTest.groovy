import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.http.client.ReceivedResponse
import ratpack.test.http.TestHttpClient
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by matt on 6/21/15.
 */
class ServiceTest extends Specification {

  @AutoCleanup
  def aut = new GroovyRatpackMainApplicationUnderTest()

  @Delegate
  TestHttpClient client = ratpack.test.http.TestHttpClient.testHttpClient(aut)

  def setup() {
    resetRequest()
  }

  @Unroll
  def "Can register profile and query by #field"() {
    when:
    requestSpec {
      it.headers.add("Content-Type", "application/json")
      it.body.stream { it << '{"firstName": "foo", "lastName": "bar", "email": "foo.bar@gmail.com"}' }
    }
    post "service/register"

    then:
    response.body.text == "1"

    when:
    get endPoint

    then:
    with(new JsonResponse(response).json) {
      "1" == id.asText()
      "foo" == firstName.asText()
      "bar" == lastName.asText()
      "foo.bar@gmail.com" == email.asText()
    }

    where:
    field   | endPoint
    "id"    | "service/id/1"
    "email" | "service/email/foo.bar@gmail.com"
  }

  class JsonResponse {
    private JsonNode json

    JsonResponse(ReceivedResponse response) {
      json = new ObjectMapper().reader().readTree(response.body.text)
    }
  }
}
