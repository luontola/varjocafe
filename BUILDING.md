
Building
========

Create executable:

    lein uberjar
    cp target/varjocafe-standalone.jar ...

Running tests:

    lein midje
    lein midje :autotest
    lein midje :autotest :filter -slow
