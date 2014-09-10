worktrail-app-hub-sync

# Synchronizes items into WorkTrail Hub

Currently supports:

1. Activity Streams (For JIRA): ActivityStreamCli
2. GIT Commits: GitSyncCli

Uses API specified at https://worktrail.net/en/api/

## Dependencies

https://github.com/worktrail/worktrail-app-api

Checkout and run:

    ./gradlew publishMavenJavaPublicationToMavenLocal

## Development

    ./gradlw eclipse

and import into your eclipse workspace.

# License

worktrail-app-api is available under The BSD 2-Clause License. Please
contribute back by sending us pull requests on github:
https://github.com/worktrail/worktrail-app-hub-sync
