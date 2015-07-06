    $ curl -H "Content-Type: application/json" -X POST -d '{"firstName": "foo", "lastName": "bar", "email": "foo.bar@gmail.com"}' http://localhost:5051/service/register
    1
    
    $ curl http://localhost:5051/service/email/foo.bar@gmail.com
    {
      "id" : 1,
      "firstName" : "foo",
      "lastName" : "bar",
      "email" : "foo.bar@gmail.com"
    }
    
    $ curl http://localhost:5051/service/id/1
    {
      "id" : 1,
      "firstName" : "foo",
      "lastName" : "bar",
      "email" : "foo.bar@gmail.com"
    }

    $ while true; do curl -H "Content-Type: application/json" -X POST -d '{"firstName": "foo", "lastName": "bar", "email": "foo.bar@gmail.com"}' http://localhost:5050/service/register 2>&1 >/dev/null & sleep .2 ; done