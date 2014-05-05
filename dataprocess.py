#!/usr/bin/python
import os

path = '/home/dhawan2/cs410/HotelReviews/reviews/'

processed = open('HotelDatabase.java', 'w')
for filename in os.listdir(path):
	if filename.endswith('.dat'):
		with open(path + filename, 'r+') as hotel:
			Content = ''

			for line in hotel:
				#print line
				if "<overall rating>" in line.lower():
					Overall_Rating = line.split('>')[1]
					#print Overall_Rating
				elif "<avg. price>" in line.lower():
					Avg_Price = line.split('>')[1]
					#print Avg_Price
				elif "<content>" in line.lower():
					Content = Content + line.split('>')[1].strip()
					#print Content
			processed.write('\n' + Overall_Rating + '\n' + Avg_Price + '\n' + Content + '\n')
			'''
			if '<url>' not in contents: 
				continue
				
			else:
				Overall_Rating = ''
				Avg_Price = '$0'
				Content = ''
				for line in hotel:
					if "<overall rating>" in line.lower():
						Overall_Rating = line.split('>')[1]
						#print "rating"
					elif "<avg. price>" in line.lower():
						Avg_Price = line.split('>')[1]
						#print "here"
					elif "<content>" in line.lower():
						#print "contents"
						Content = Content + line.split('>')[1]
				processed.write(Overall_Rating + '\n' + Avg_Price + '\n' + Content )
				'''



