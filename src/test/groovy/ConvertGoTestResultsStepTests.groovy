// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue

class ConvertGoTestResultsStepTests extends ApmBasePipelineTest {
  String scriptName = 'vars/convertGoTestResults.groovy'

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()
  }

  @Test
  void test() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('withMageEnv', [Closure.class], { b -> b()  })
    helper.registerAllowedMethod('sh', [Map.class], { m -> m.script  })
    script.call(input: "dummyIN", output: "dummyOUT")
    printCallStack()
    assertTrue(assertMethodCallContainsPattern('sh', 'go-junit-report > dummyOUT < dummyIN'))
    assertTrue(assertMethodCallContainsPattern('junit', 'testResults=dummyOUT'))
    assertJobStatusSuccess()
  }

  @Test
  void testNoInput() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('withMageEnv', [Closure.class], { b -> b()  })
    helper.registerAllowedMethod('sh', [Map.class], { m -> m.script  })
    try {
      script.call(output: "dummyOUT")
    } catch(e){
      assertTrue(e.getMessage() == "convertGoTestResults: missing input file")
    }
    printCallStack()
    assertJobStatusFailure()
  }

  @Test
  void testNoOutput() throws Exception {
    def script = loadScript(scriptName)
    helper.registerAllowedMethod('withMageEnv', [Closure.class], { b -> b()  })
    helper.registerAllowedMethod('sh', [Map.class], { m -> m.script  })
    try {
      script.call(input: "dummyIN")
    } catch(e){
      assertTrue(e.getMessage() == "convertGoTestResults: missing output file")
    }
    printCallStack()
    assertJobStatusFailure()
  }
}
