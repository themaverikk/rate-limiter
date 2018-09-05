Rate Limiter is a maven project built on Spring Boot and written in Java 8. It is meant to be run as separate micro service on a single JVM. All client facing services can call it to verify whether a client call should be permitted or not. Alterations are required to run it as micro service with multiple instances (multiple JVMs) or to couple it individually with multiple services.
It uses Token bucket algorithm to enforce configured limits on the no. of API calls that can be made within a stipulated interval of time. This implied we are given with certain permits or tokens which increase as a function of time. Whenever a client tries to call an API, it consumes one permit out of the bucket. So the call goes though only if no. of permits available exceed 0. Number of available permits keep on increasing in bucket as the time passes.


Assumptions:
1. Application assumes that this is B2C application, hence the no. of clients accessing the application is very high. Also, since we are enforcing various limits on a single client (I.e. Global, Method, API, SEC, MIN, HOUR, WEEK, MONTH), application is a little lenient about smaller limits. e.g. If client has a limit of 5 requests per minute, there may arise some scenario where client was able to hit 7 times within a sliding window of 1 minute, however it won't be able to cross average no. of calls permitted over large duration of time. But since we have multiple limits configured for a single client eg. SEC, MIN, HOUR etc, the scope of crossing the limit will be limited.
This is a deliberate tradeoff to reduce the memory footprint of the application. If we were to enforce stricter limits, that means keeping track of timestamps of n requests for a single limit for a single client (limit is n requests per time interval).\
If there are c clients, each client has 15 limits (as in our case) and on average we are allowing n requests per time-duration. Then memory usage will be O(c * 15 * n), which can be pretty huge when we have millions of clients (or customers). So application allows a limited scope of error to save the memory.

2. Ideally application should use some In-Memory DB (Redis or Aerospike), to avoid installation of In-Memory DB, it uses H2 database which is a RDBMS and uses RAM for persistence. H2 doesn\'92t require any installation on device and can be used only by including maven dependency. It is a perfect fit for assignment. However, In production H2 will be replaced with some cache framework working on key-value pairs. So I H2 tables are not normalised to keep the database structure similar to Key-value pair and also to eliminate the need for table joins, which make application slower.

3. It is assumed our application will be running as a separate micro service on a single JVM. Whenever any client hits some particular API of other micro service, that micro service will call Rate-Limiter to ascertain whether the call should be permitted or not. Inherently there will be some network delay. In case this is not acceptable, this application, being a spring boot application, can be combined with any micro service to throttle the traffic. But in that case locking mechanism should be implemented on DB (like Redis distributed locking) since current JVM based locking will not be sufficient to ensure proper throttling across distributed systems.

4. No. of clients is high, but not high enough to degrade the performance of HashMap used for client locks. Also addition of clients is rare. Application instantiates a new HashMap every time a client is added/updated and copies data from previous map, then adds the current client. This is done to eliminate the locking required, every time a client requests lock, which would be very frequent. In case clients are frequently updated, HashMap should be replaced with thread safe implementation like ConcurrentHashMap, but that would make client lock acquisition and release slower. Also, if no. of clients is too high to fit the client-lock data into RAM (HashMap), some DB specific mechanism should be used in place of HashMap based locking implementation.

5. Since it is assumed client is not directly calling this service, but is calling some other micro service, which in turn is calling Rate-Limiter to determine validity of client request, application returns results with Status SUCCESS and FAILURE with HTTP status code 200. Responding to client with appropriate HTTP status code (like 429) has been left at the discretion of the client-facing service.

Algorithm and concept:

Suppose there's a limit configured for some API, say 5 calls per minute. For this limit, application tracks the following things.
1. maxPermits -> maximum calls that are permitted in the given time interval (5 for the current case).
2. availablePermits -> calls that are remaining
3. lastCallTimestamp -> timestamp of the last call that went through.
4. rate limit configured -> say for our current scenario (5 calls per minute), Initially the maxPermits as well as the availablePermits will be 5 and we take lastCallTimestamp as the timestamp of the system at the time of limit configuration.\

If (API call):
	replenishedPermits = r * (currentTimestamp - lastCallTimestamp) in minutes;

	availablePermits = availablePermits + replenishedPermits;
	if (availablePermits > maxPermits):
	  availablePermits = maxPermits;

		if(availablePermits  >  0):
		  availablePermits = availablePermits - 1;
		  lastCallTimestamp = currentTimestamp;
		  call goes through

	else:
		call blocked

Working:
	In addition to the above algorithm, we need to handle concurrent requests by a particular client. Hence some form of locking is required. Application uses a Map with keys as client-id and value as instances of ReentrantLock. Whenever a client is requesting access to some API, a lock will be taken on that client-id and all limits will be checked in sequence (API, METHOD, DEFAULT) with time durations (SEC, MIN, HOUR, WEEK, MONTH). After the decision is made for client to either allow or block the call, lock will be released for the client-id. Effectively whole process which decides whether to allow or block the call is treated like a transaction. In case the call goes through results will be persisted in DB, else rollbacked.

If a request is already in process for some given client, subsequent requests from that client will keep on waiting for the former request to complete. This is necessary to avoid lost update problem and race conditions, because there may be as many as 15 limits to be checked for a single client request and only if request conforms with all the limits, it will be allowed and results will be persisted. So this is not possible to fulfil other request concurrently for a given client.\
Locking mechanism is implemented using a immutable HashMap (not thread-safe) with volatile reference (for safe publishing of the reference). Application makes sure that the contents of Hashmap is never changed after it has been published. Only reference can be updated, which is volatile. Whenever a new client is configured to the system, contents of the old HashMap is copied into the new Hashmap followed by addition of the current client. Now old HashMap reference is replaced with the updated HashMap. Since this is prone to lost update problem, operation of adding a new client to the system has been serialised (taking lock on the service instance). But since we expect client addition to be the rare operation, its impact can be ignored. But this approach really shines when we are acquiring lock for a particular client-id which is a very frequent operation. Since non thread-safe HashMap places no locking overhead for reading the values, lock acquisition and release are more efficient.


Improvements:
1. Adding/editing/deleting limits for a client, once client has been added.
2. Replacing H2 database with cache framework.
3. Replacing locking mechanism with Distributed DB locking in case application is deployed on multiple JVMs.
4. Altering locking mechanism in case addition of clients is frequent or if the no. of clients is too high to fit in the system, in which case the active clients can be kept into RAM and dormant can be fetched from DB when required.


APIs:
1. Add client and limits
curl -X POST \
  http://localhost:8080/throttling/configure-client \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -d '{
	"clientId": "test_client",
	"limits": [{
			"limitType": "DEFAULT",
			"limitName": "GLOBAL",
			"timeIntervalLimits": [{
					"timeUnit": "SEC",
					"maxRequests": 10
				},
				{
					"timeUnit": "MIN",
					"maxRequests": 20
				},
				{
					"timeUnit": "HOUR",
					"maxRequests": 60
				}
			]

		},
		{
			"limitType": "METHOD",
			"limitName": "GET",
			"timeIntervalLimits": [{
					"timeUnit": "SEC",
					"maxRequests": 10
				},
				{
					"timeUnit": "MIN",
					"maxRequests": 20
				},
				{
					"timeUnit": "HOUR",
					"maxRequests": 60
				}
			]

		},
		{
			"limitType": "METHOD",
			"limitName": "POST",
			"timeIntervalLimits": [{
					"timeUnit": "SEC",
					"maxRequests": 10
				},
				{
					"timeUnit": "MIN",
					"maxRequests": 20
				},
				{
					"timeUnit": "HOUR",
					"maxRequests": 60
				}
			]

		},
		{
			"limitType": "API",
			"limitName": "/test",
			"timeIntervalLimits": [{
					"timeUnit": "SEC",
					"maxRequests": 10
				},
				{
					"timeUnit": "MIN",
					"maxRequests": 20
				},
				{
					"timeUnit": "HOUR",
					"maxRequests": 60
				}
			]

		},
		{
			"limitType": "API",
			"limitName": "/status",
			"timeIntervalLimits": [{
					"timeUnit": "SEC",
					"maxRequests": 10
				},
				{
					"timeUnit": "MIN",
					"maxRequests": 20
				},
				{
					"timeUnit": "HOUR",
					"maxRequests": 60
				}
			]

		}
	]

}'

2. Retrieve all configured limits in the system
curl -X GET \
  http://localhost:8080/throttling/configured-limits \
  -H 'Cache-Control: no-cache' \

3. Verify whether client call is permitted or not (throttling)
curl -X POST \
  http://localhost:8080/throttling/verify-api-limit \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -d '{
	"clientId" : "test_client",
	"methodName" : "GET",
	"apiName" : "/test"
}'