package com.aimprosoft.free.grammar

import com.aimprosoft.model.TIdentity

/**
 * [[Op]] represents a set of operations for creating, updating,
 * deleting, and reading records parametrized by the model type.
 *
 * @tparam M Model type
 * @tparam R Result type
 */
trait Op[M <: TIdentity, R]