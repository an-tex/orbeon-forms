/**
 * Copyright (C) 2014 Orbeon, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.fr

import org.orbeon.oxf.test.TestHttpClient.{CacheEvent, StaticState}
import org.orbeon.oxf.test.{DocumentTestBase, ResourceManagerSupport}
import org.orbeon.oxf.xforms.state.XFormsStaticStateCache
import org.scalatest.FunSpecLike

class CacheTest
  extends DocumentTestBase
     with ResourceManagerSupport
     with FunSpecLike
     with FormRunnerSupport{

  describe("Form Runner static cache") {

    val Id1 = "6578e2e0e7911fd9ba284aefaea671cbfb814851"
    val Id2 = "15c4a18428496faa1212d86f58c62d9d3c51cf0d"

    def runAndAssert(form: String, mode: String, noscript: Boolean)(expectedInitialHit: Boolean, staticStateHoldsTemplate: Boolean) = {

      def staticStateFoundOpt(events: List[CacheEvent]) =
        events collectFirst { case StaticState(found, _) ⇒ found }

      def staticStateHasTemplateOpt(events: List[CacheEvent]) = (
        events
        collectFirst { case StaticState(_, digest) ⇒ digest}
        flatMap XFormsStaticStateCache.findDocument
        map (_.template.isDefined)
      )

      // First time may or may not pass
      val (_, _, events1) = runFormRunner("tests", form, mode, document = Id1, noscript = noscript, initialize = true)
      it(s"initial hit `$expectedInitialHit` for $form/$mode/$noscript") {
        assert(Some(expectedInitialHit) === staticStateFoundOpt(events1))
        // NOTE: no XFCD because the form has `xxf:no-updates="true"`.
      }

      // Second time with different document must always pass
      val (_, _, events2) = runFormRunner("tests", form, mode, document = Id2, noscript = noscript, initialize = true)
      it(s"second hit `true` for $form/$mode/$noscript") {
        assert(Some(true) === staticStateFoundOpt(events2))
        // NOTE: no XFCD because the form has `xxf:no-updates="true"`.
      }

      it(s"template to `$staticStateHoldsTemplate` for $form/$mode/$noscript") {
        assert(Some(staticStateHoldsTemplate) === staticStateHasTemplateOpt(events2))
      }
    }

    locally {
      val Form = "noscript-true-pdf-auto-wizard-false"
      val staticStateHoldsTemplate = true

      runAndAssert(Form, "new", noscript = false)(expectedInitialHit = false, staticStateHoldsTemplate)

      for (mode ← Seq("edit", "view", "pdf"))
        runAndAssert(Form, mode, noscript = false)(expectedInitialHit = true, staticStateHoldsTemplate)

      // Once #1712 is fixed, should return true
      // See https://github.com/orbeon/orbeon-forms/issues/1712
      runAndAssert(Form, "edit", noscript = true)(expectedInitialHit = false, staticStateHoldsTemplate)
    }

    locally {
      val Form = "noscript-false-pdf-template-wizard-true"
      val staticStateHoldsTemplate = false

      runAndAssert(Form, "new" , noscript = false)(expectedInitialHit = false, staticStateHoldsTemplate)
      runAndAssert(Form, "edit", noscript = false)(expectedInitialHit = true,  staticStateHoldsTemplate)
      runAndAssert(Form, "view", noscript = false)(expectedInitialHit = false, staticStateHoldsTemplate)
      runAndAssert(Form, "pdf" , noscript = false)(expectedInitialHit = true,  staticStateHoldsTemplate)

      runAndAssert(Form, "edit", noscript = true)(expectedInitialHit = true, staticStateHoldsTemplate)
    }
  }
}
