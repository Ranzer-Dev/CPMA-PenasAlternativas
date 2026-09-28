const { spawn } = require('child_process');
const path = require('path');

const isWindows = process.platform === 'win32';
const target = process.argv[2] || 'admin';
const rootDir = path.resolve(__dirname, '..');

let cmd;
let args = [];

if (target === 'admin') {
    if (isWindows) {
        cmd = path.join(rootDir, 'start_admin.bat');
    } else {
        cmd = 'bash';
        args = [path.join(rootDir, 'scripts', 'start_admin.sh')];
    }
} else if (target === 'totem') {
    if (isWindows) {
        cmd = path.join(rootDir, 'start_totem.bat');
    } else {
        cmd = 'bash';
        args = [path.join(rootDir, 'scripts', 'start_totem.sh')];
    }
} else if (target === 'test') {
    if (isWindows) {
        cmd = path.join(rootDir, 'test_all.bat');
    } else {
        cmd = 'bash';
        args = [path.join(rootDir, 'scripts', 'test_all.sh')];
    }
}

const child = spawn(cmd, args, {
    stdio: 'inherit',
    cwd: rootDir,
    shell: isWindows
});

child.on('exit', (code) => {
    process.exit(code || 0);
});
