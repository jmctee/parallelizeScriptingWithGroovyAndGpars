#!/usr/bin/env groovy

@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')

import static groovyx.gpars.GParsPool.withPool

def inputFile = new File(args[0])

withPool(Integer.parseInt(args[2])) {
  inputFile.eachParallel { line ->
    def commandArray = ["lookupSoaDemographicsSummary.sh","${line}",args[1]]
    def proc = commandArray.execute()
    proc.waitFor()
    print proc.text
  }
}
