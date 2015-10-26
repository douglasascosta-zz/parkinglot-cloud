#!/usr/bin/python

import sys
from random import randint

if (len(sys.argv) > 1):
	number = int(sys.argv[1])
else:
	print "Invalid number of parking lots"
	exit();

print "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
print "<additional>"

for i in range(number):
	print "\t<poi id=\"%d\" lane=\":%d/%d_0_0\" pos=\"0\"/>" % (i, randint(0,9), randint(0,9))

print "</additional>"