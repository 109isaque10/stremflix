package com.stremflix.core.domain.usecase

import com.stremflix.core.domain.model.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

abstract class UseCase<in Params, ResultType>(
    private val dispatcher: CoroutineDispatcher
) {
    abstract suspend fun execute(params: Params): Result<ResultType>

    suspend operator fun invoke(params: Params): Result<ResultType> =
        withContext(dispatcher) { execute(params) }
}

abstract class FlowUseCase<in Params, ResultType>(
    private val dispatcher: CoroutineDispatcher
) {
    protected abstract fun buildFlow(params: Params): Flow<Result<ResultType>>

    operator fun invoke(params: Params): Flow<Result<ResultType>> =
        buildFlow(params).flowOn(dispatcher)
}

abstract class SimpleUseCase<ResultType>(
    private val dispatcher: CoroutineDispatcher
) {
    abstract suspend fun execute(): Result<ResultType>

    suspend operator fun invoke(): Result<ResultType> =
        withContext(dispatcher) { execute() }
}

abstract class SimpleFlowUseCase<ResultType>(
    private val dispatcher: CoroutineDispatcher
) {
    protected abstract fun buildFlow(): Flow<Result<ResultType>>

    operator fun invoke(): Flow<Result<ResultType>> =
        buildFlow().flowOn(dispatcher)
}

// No-params variants
abstract class NoParamsUseCase<ResultType>(dispatcher: CoroutineDispatcher) :
    SimpleUseCase<ResultType>(dispatcher)

abstract class NoParamsFlowUseCase<ResultType>(dispatcher: CoroutineDispatcher) :
    SimpleFlowUseCase<ResultType>(dispatcher)