/*
 * Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.lagom.internal.registry

import java.net.URI

import akka.actor.ActorSystem
import akka.discovery.SimpleServiceDiscovery.{ Resolved, ResolvedTarget }
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{ Matchers, WordSpecLike }

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._

class DevModeSimpleServiceDiscoverySpec
  extends TestKit(ActorSystem("DevModeSimpleServiceDiscoverySpec"))
  with WordSpecLike
  with Matchers {

  private val client = new StaticServiceRegistryClient(
    Map(
      "test-service" -> List(URI.create("http://localhost:8080")),
      "test-service-without-port" -> List(URI.create("http://localhost"))
    )
  )
  private val discovery = DevModeSimpleServiceDiscovery(system)
  discovery.setServiceRegistryClient(client)

  "DevModeSimpleServiceDiscoverySpec" should {
    "resolve services in the registry" in {
      val expected = Resolved("test-service", List(ResolvedTarget("localhost", Some(8080))))
      discovery.lookup("test-service", 100.milliseconds).futureValue shouldBe expected
    }

    "allow missing ports" in {
      val expected = Resolved("test-service-without-port", List(ResolvedTarget("localhost", None)))
      discovery.lookup("test-service-without-port", 100.milliseconds).futureValue shouldBe expected
    }
  }

}

private class StaticServiceRegistryClient(registrations: Map[String, List[URI]]) extends ServiceRegistryClient {
  override def locateAll(serviceName: String): Future[immutable.Seq[URI]] =
    Future.successful(registrations.getOrElse(serviceName, Nil))
}
