import sys, os, base64
file_path = base64.b64decode(sys.argv[1]).decode('utf-8')
content = base64.b64decode(sys.argv[2]).decode('utf-8')
os.makedirs(os.path.dirname(os.path.abspath(file_path)), exist_ok=True)
with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print(f'OK: {file_path}')
