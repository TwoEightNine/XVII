import json

template = """
codes.add("%s")
resources.add("%s")
"""
f = open("code.kt", "w")
valid = json.load(open("/home/twoeightnine/emojis/validEmojis", "rb"))

for k in valid:
	f.write(template % (valid[k], k))

f.close()