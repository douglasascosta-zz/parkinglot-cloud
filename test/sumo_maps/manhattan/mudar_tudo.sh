#!/bin/bash  

trafego=200
repeticao=0

while [ $trafego -lt 600 ]; do
	repeticao=0
	while [ $repeticao -lt 50 ]; do
		sed  '/html/d' $trafego"_"$repeticao.rou.xml | sed  '/body/d' >teste.txt
		mv teste.txt $trafego"_"$repeticao.rou.xml
		
		sed  '/html/d' $trafego"_"$repeticao.launchd.xml | sed  '/body/d' >teste2.txt
		mv teste2.txt $trafego"_"$repeticao.launchd.xml

		sed  '/html/d' $trafego"_"$repeticao.sumo.cfg | sed  '/body/d' >teste3.txt
		mv teste3.txt $trafego"_"$repeticao.sumo.cfg

		let repeticao=repeticao+1
	done
	let trafego=trafego+100
done
