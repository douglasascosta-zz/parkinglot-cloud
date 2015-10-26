#!/usr/bin/python

import sys
from random import randint

if (len(sys.argv) > 1):
	number = int(sys.argv[1])
else:
	print "Invalid number of parking lots"
	exit();

vagasFile = "vagas.txt"

file = open(vagasFile, "w");
file.write(str(randint(0,number)))
file.close();
