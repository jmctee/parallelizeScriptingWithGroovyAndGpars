#!/usr/bin/env groovy

@Grab(group='org.codehaus.gpars', module='gpars', version='1.2.1')
@Grab(group='com.opencsv', module='opencsv', version='3.3') 

import static groovyx.gpars.GParsPool.withPool
import com.opencsv.CSVWriter

def numberOfFilesToCreate = Integer.parseInt(args[0]) // # of files to create
def linesPerFile = Integer.parseInt(args[1]) // # of lines per file
def filesPerZip = Integer.parseInt(args[2])
def threadPoolSize = Integer.parseInt(args[3])

withPool(threadPoolSize) {
  (0..<numberOfFilesToCreate).each { index ->
	  def fileName = "data_${index}.csv"
	  def writer = new CSVWriter(new FileWriter(fileName))
		(0..<linesPerFile).each { line ->
		  String[] entries = ["name_${index}_${line}","${index}","${line}"]
		  writer.writeNext(entries)
		}
	  writer.close()
  }
}

def commandArray = []

def archiveIndex = 0
def command = ["zip", "archive_${archiveIndex}.zip"]
commandArray << command

(0..<numberOfFilesToCreate).each { index ->
  def fileName = "data_${index}.csv"
	command << fileName
	
	if (command.size()-2 >= filesPerZip) {
		archiveIndex++
		command = ["zip", "archive_${archiveIndex}.zip"]
		commandArray << command
	}
}

withPool(threadPoolSize) {
  commandArray.each { commandLine ->
	    def proc = commandLine.execute()
	    proc.waitFor()
	}
}
