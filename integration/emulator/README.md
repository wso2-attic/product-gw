

Mock Server
===========

 Emulator.getProtocolEmulator("http")
        .consumer()
        .host("localhost")
        .port("6060)
        .readingDelay(10)
        .writingDelay(3)
        .randomConnectionClose(true)
        .httpVersion("1.1")
        .context("/AccountService")
        .when(
                request()
                        .withMethod("POST")
                        .withPath("/login")
                        .withBody("{username: 'foo', password: 'bar'}")
        )
        .logic(new BusinessLogicHandler())
        .respond(
                response()
                        .withStatusCode(302)
                        .withHeader(
                                "Location", "https://www.wso2.com"
                        )
                        .withBody("{ message: 'hello emulator'}")
        )
        .start();

Mock Client
===========

