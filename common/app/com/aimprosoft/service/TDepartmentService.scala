package com.aimprosoft.service

import com.aimprosoft.model._

trait TDepartmentService[F[_], K] extends TBasicService[F, K, Department[K]]