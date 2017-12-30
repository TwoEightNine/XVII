import json
import os

path = "/home/twoeightnine/emojis3/"
files = os.listdir(path)

valid = json.load(open("/home/twoeightnine/emojis/validEmojis", "rb"))
print len(valid)

names = [k for k in valid]
# print names

for file in files:
	if file not in names:
		os.remove(path + file)