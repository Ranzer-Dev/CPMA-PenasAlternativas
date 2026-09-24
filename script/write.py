import sys, os
path = sys.argv[1]
os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
data = sys.stdin.buffer.read().decode('utf-8', errors='replace').lstrip('\ufeff')
with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(data)
print(f'WRITTEN: {path}')
