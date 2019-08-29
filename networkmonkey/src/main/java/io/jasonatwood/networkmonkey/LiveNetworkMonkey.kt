package io.jasonatwood.networkmonkey

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Random

class LiveNetworkMonkey : NetworkMonkey {

    private val enabledOperations: MutableSet<Operation> = HashSet()
    private var jerkMode = false
    private var testMode = false

    override fun shouldMonkeyWithWifiConnection() {
        println("Warning: NetworkMonkey no longer monkeys with wifi connection. This will be " +
                "removed in later versions of NetworkMonkey")
    }

    override fun shouldMonkeyWithResponseCode(code: Int) {
        enabledOperations.add(
                Operation(String.format("Return %d on any request", code),
                        Operation.Method.ANY,
                        null,
                        1,
                        false,
                        null,
                        { initialResponse: Response ->
                            initialResponse.newBuilder().code(code).build()
                        }))
    }

    override fun shouldMonkeyWithResponseTime(delayInMilliseconds: Int) {
        enabledOperations.add(
                Operation(String.format("Delay any request by %d milliseconds", delayInMilliseconds),
                        Operation.Method.ANY,
                        null,
                        1,
                        false,
                        null,
                        { initialResponse: Response? ->
                            try {
                                Thread.sleep(delayInMilliseconds.toLong())
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            initialResponse!!
                        }))
    }

    override fun shouldMonkeyWithRequestSuccess() {
        enabledOperations.add(
                Operation("Throw error on any request",
                        Operation.Method.ANY,
                        null,
                        1,
                        false,
                        null,
                        { throw  IOException("Monkey Exception") })
        )
    }

    override fun enableJerkMode() {
        jerkMode = true
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        if (enabledOperations.isEmpty()) {
            return chain.proceed(request)
        }

        // find all matching operations
        val matchingOperations: MutableList<Operation> = ArrayList()
        for (operation in enabledOperations) {
            if (operation.matches(request)) {
                matchingOperations.add(operation)
            }
        }

        // possible we have no matching operations
        if (matchingOperations.isEmpty()) {
            return chain.proceed(request)
        }

        // are any of these matching operations mandatory?
        var selectedOperation: Operation? = null
        for (operation in matchingOperations) {
            if (operation.mandatory) {
                selectedOperation = operation
                break
            }
        }

        // check probability and mandatory
        if (!shouldRandomlyMonkey() && selectedOperation == null) {
            return chain.proceed(request)
        }

        // no operations are mandatory and we rolled the dice, so randomly pick operation based on weight
        if (selectedOperation == null) {
            selectedOperation = pickRandomOperationBasedOnWeight(matchingOperations)
        }

        selectedOperation!!.monkeyWithRequest(request)

        // pass on to the rest of the chain
        val response = chain.proceed(request)

        return selectedOperation.monkeyWithResponse(response)
    }

    /**
     * For testing only
     */
    fun enableTestMode() {
        testMode = true
    }

    /**
     * @return true if we should monkey with this operation based on randomizer
     */
    private fun shouldRandomlyMonkey(): Boolean {
        if (testMode) {
            return true // in test we want to always monkey
        } else if (jerkMode) {
            return System.currentTimeMillis() % 2 == 0L // 1:2 chance of doing something
        }
        return System.currentTimeMillis() % 10 == 0L // 1:10 chance of doing something
    }

    private fun pickRandomOperationBasedOnWeight(operations: List<Operation>): Operation? {
        var sumOfWeights = 0
        for (operation in operations) {
            sumOfWeights += operation.weight
        }
        val randomInt = Random().nextInt(sumOfWeights)
        var counter = 0
        for (operation in operations) {
            counter += operation.weight
            if (randomInt < counter) {
                return operation
            }
        }
        throw RuntimeException("Failed to pick random operation:"
                + " randomInt: " + randomInt + "; " + "total weight: " + sumOfWeights)
    }
}