// shims node test env to have a window global, so browser libs that aren't
// headless-friendly don't freak out.
// pull in as a foreign lib in compiler options in tests.edn
const Window = require('window');
window = new Window();
console.log("window is", window);
