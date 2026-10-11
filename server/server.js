const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const https = require('https');
const app = express();
const upload = multer({ dest: '/tmp/snaprelay', limits: { fileSize: 10 * 1024 * 1024 } });
const PORT = process.env.PORT || 8080;
const TOKEN = process.env.TELEGRAM_BOT_TOKEN;
const CHAT_ID = process.env.TELEGRAM_CHAT_ID;
function telegramSendPhoto(filePath, caption) {
  return new Promise((resolve, reject) => {
    const boundary = '----SnapRelay' + Date.now();
    const file = fs.readFileSync(filePath);
    const body = Buffer.concat([
      Buffer.from(`--${boundary}\\r\\nContent-Disposition: form-data; name="chat_id"\\r\\n\\r\\n${CHAT_ID}\\r\\n`),
      Buffer.from(`--${boundary}\\r\\nContent-Disposition: form-data; name="caption"\\r\\n\\r\\n${caption || 'SnapRelay capture'}\\r\\n`),
      Buffer.from(`--${boundary}\\r\\nContent-Disposition: form-data; name="photo"; filename="capture.jpg"\\r\\nContent-Type: image/jpeg\\r\\n\\r\\n`), file,
      Buffer.from(`\\r\\n--${boundary}--\\r\\n`)
    ]);
    const req = https.request({ hostname: 'api.telegram.org', path: `/bot${TOKEN}/sendPhoto`, method: 'POST', headers: {'Content-Type': `multipart/form-data; boundary=${boundary}`, 'Content-Length': body.length} }, r => { let data=''; r.on('data', c => data += c); r.on('end', () => r.statusCode >= 200 && r.statusCode < 300 ? resolve(data) : reject(new Error(data))); });
    req.on('error', reject); req.write(body); req.end();
  });
}
app.get('/health', (_, res) => res.json({ ok: true }));
app.post('/upload', upload.single('photo'), async (req, res) => {
  if (!TOKEN || !CHAT_ID) return res.status(500).json({ error: 'Telegram server configuration missing' });
  if (!req.file) return res.status(400).json({ error: 'Photo required' });
  try { await telegramSendPhoto(req.file.path, req.body.caption); res.json({ ok: true }); }
  catch (e) { res.status(502).json({ error: 'Telegram delivery failed' }); }
  finally { fs.unlink(req.file.path, () => {}); }
});
app.listen(PORT, () => console.log(`SnapRelay API listening on ${PORT}`));
