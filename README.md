# The RelationalAI Software Development Kit for Java

The RelationalAI (RAI) SDK for Java enables developers to access the RAI REST APIs from Java.

* You can find RelationalAI Java SDK documentation at <https://docs.relational.ai/rkgms/sdk/java-sdk>
* You can find RelationalAI product documentation at <https://docs.relational.ai>
* You can learn more about RelationalAI at <https://relational.ai>

## Getting started

### Requirements

* Java 11.0.10+
* Apache Maven

### Building the SDK

The SDK has a single runtime dependency (jsoniter) and several additional
dependencies for running the unit and integratin tests.

The SDK build lifecycle is managed using the standard `mvn` lifecycle commands.

Compile the SDK source code

    mvn compile

Run the unit tests

    mvn test

Compile, run units and build that SDK jar file `rai-sdk-java.jar`

    mvn package

The `package` command will also copy runtime dependencies into the target
folder.

### Create a configuration file

In order to run the examples you will need to create an SDK config file.
The default location for the file is `$HOME/.rai/config` and the file should
include the following:

Sample configuration using OAuth client credentials:

    [default]
    host = azure.relationalai.com
    port = <api-port>      # optional, default: 443
    scheme = <scheme>      # optional, default: https
    client_id = <your client_id>
    client_secret = <your client secret>
    client_credentials_url = <account login URL>  # optional
    # default: https://login.relationalai.com/oauth/token

Client credentials can be created using the RAI console at
https://console.relationalai.com/login

You can copy `config.spec` from the root of this repo and modify as needed.

## Examples

Each of the examples in the `./examples` folder can be run from the command
line. There are many ways to do this, the following is one method for running
examples from the root of the repo without installing dependencies.

Build the SDK jar file and copy dependencies with `mvn package`.

Compile the example `examples/ListDatabases.java`

    javac -cp "./target/*" -d "./target/classes" examples/ListDatabases.java

Run the example `ListDatabases`

    java -cp "./target/classes:./target/*:./target/dependency/*" ListDatabases

## Support

You can reach the RAI developer support team at `support@relational.ai`

## Contributing

We value feedback and contributions from our developer community. Feel free
to submit an issue or a PR here.

## License

The RelationalAI Software Development Kit for Julia is licensed under the
Apache License 2.0. See:
https://github.com/RelationalAI/rai-sdk-julia/blob/master/LICENSE
