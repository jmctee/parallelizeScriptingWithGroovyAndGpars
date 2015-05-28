#!/usr/bin/env groovy

@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')
@Grab(group='com.opencsv', module='opencsv', version='3.3') 

import static groovyx.gpars.GParsExecutorsPool.withPool
import com.opencsv.CSVWriter
import com.opencsv.CSVReader

def unzipAndList = { line ->
	def unzipProc = ["unzip", line].execute()
	unzipProc.waitFor()

	if (unzipProc.exitValue()) {
		println unzipProc.err.text
		System.exit(-1)
	}			
	
	def fileNames = []

	unzipProc.text.eachLine { procLine ->
		if (procLine =~ /csv/) {
			fileNames << procLine.split(':')[1].trim()
		}
	}
	
	fileNames
}

def threadPoolSize = Integer.parseInt(args[0])

def lsCommand = ["sh", "-c", "ls -1 *.zip"]
def lsProc = lsCommand.execute()
lsProc.waitFor()

if (lsProc.exitValue()) {
	println lsProc.err.text
	System.exit(-1)
}

def lines = []
lsProc.text.eachLine { line ->
	lines << line
}

def fileNames = []
withPool(threadPoolSize) {
  fileNames = lines.collectParallel { unzipAndList(it) }.flatten()
}

withPool(threadPoolSize) {
	fileNames.each { fileName ->
		String psvFileName = fileName - ".csv" + ".psv"
		char seperator = '|'
		CSVReader reader = new CSVReader(new FileReader(fileName))
	  def writer = new CSVWriter(new FileWriter(psvFileName), seperator)

		String [] nextLine
		while ((nextLine = reader.readNext()) != null) {
			writer.writeNext(nextLine)
		}
		writer.close()

		def csvFile = new File(fileName)
		csvFile.delete()
	}
}
