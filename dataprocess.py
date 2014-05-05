#!/usr/bin/python
import os

path = '~/cs410/HotelReviews/reviews/'

processed = open('HotelDatabase.java', 'w')
	for filename in os.listdir(path):
			if filename.endswith('.dat'):
				
