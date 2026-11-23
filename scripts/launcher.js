const { spawn } = require('child_process');
const path = require('path');

const target = process.argv[2] || 'admin';
const rootDir = path.resolve(__dirname, '..');

let cmd;
if (target === 'admin') {
    cmd = path.join(rootDir, 'start_admin.bat');
} else if (target === 'totem') {
    cmd = path.join(rootDir, 'start_totem.bat');
} else if (target === 'test') {
    cmd = path.join(rootDir, 'test_all.bat');
} else {
    console.error(`Alvo desconhecido: ${target}. Use: admin, totem ou test.`);
    process.exit(1);
}

const child = spawn(cmd, [], {
    stdio: 'inherit',
    cwd: rootDir,
    shell: true
});

child.on('exit', (code) => {
    process.exit(code || 0);
});
