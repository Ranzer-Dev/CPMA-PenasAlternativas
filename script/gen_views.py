import os

def write_file(rel_path, content):
    base_dir = 'd:/Projetos/CPMA-PenasAlternativas/cpma-desktop'
    full_path = os.path.join(base_dir, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8', newline='\n') as out:
        out.write(content)
    print(f'Wrote {full_path}')

print('gen_views module created')
