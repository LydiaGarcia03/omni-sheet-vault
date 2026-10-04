# Regenerates icon-index.html from every SVG in ./frames. Run this after adding,
# removing, or renaming an icon file so the index stays accurate. Usage:
#   powershell -ExecutionPolicy Bypass -File .\build-icon-index.ps1
# (run from anywhere - paths are resolved relative to this script's own location)

$framesDir = Join-Path $PSScriptRoot "frames"
$outFile = Join-Path $PSScriptRoot "icon-index.html"

function Get-Category($name) {
    if ($name -match '^dnd_frame_') { return "Frame panels" }
    if ($name -eq 'dnd_frame.svg' -or $name -eq 'dnd_frame_old.svg') { return "Frame panels" }
    if ($name -match '^dnd_icon_attack_') { return "Attack categories" }
    if ($name -match '^dnd_icon_damage_') { return "Damage types" }
    if ($name -match '^dnd_icon_school_') { return "Spell schools" }
    if ($name -match '^dnd_icon_die_') { return "Dice faces" }
    if ($name -match 'rest') { return "Rest" }
    if ($name -match '^dnd_icon_marker_') { return "Spell markers" }
    if ($name -match '^dnd_icon_ability_') { return "Ability scores (staged, not yet applied)" }
    if ($name -match '^dnd_icon_condition_') { return "Conditions (staged, not yet applied)" }
    if ($name -match '^dnd_icon_coin_') { return "Coins (staged, not yet applied)" }
    if ($name -match '^dnd_icon_aoe_') { return "Area of effect shapes" }
    if ($name -match '^dnd_icon_proficiency_') { return "Proficiency dots (staged, not yet applied)" }
    return "Misc icons"
}

function HtmlEscape($s) {
    return $s -replace '&','&amp;' -replace '<','&lt;' -replace '>','&gt;'
}

$files = Get-ChildItem -Path $framesDir -Filter "*.svg" | Sort-Object Name

$categoryOrder = @("Attack categories","Damage types","Spell schools","Spell markers","Area of effect shapes","Dice faces","Rest","Misc icons","Frame panels","Ability scores (staged, not yet applied)","Conditions (staged, not yet applied)","Proficiency dots (staged, not yet applied)","Coins (staged, not yet applied)")
$byCategory = @{}
foreach ($f in $files) {
    $cat = Get-Category $f.Name
    if (-not $byCategory.ContainsKey($cat)) { $byCategory[$cat] = @() }
    $byCategory[$cat] += $f
}

$sections = New-Object System.Text.StringBuilder

foreach ($cat in $categoryOrder) {
    if (-not $byCategory.ContainsKey($cat)) { continue }
    [void]$sections.AppendLine("<section class=`"cat`">")
    [void]$sections.AppendLine("<h2>$cat</h2>")
    [void]$sections.AppendLine("<div class=`"grid`">")
    foreach ($f in ($byCategory[$cat] | Sort-Object Name)) {
        $raw = Get-Content -Path $f.FullName -Raw
        $rawTrim = $raw.Trim()
        $escaped = HtmlEscape $rawTrim
        $importPath = "./frames/$($f.Name)"
        $card = @"
  <article class="card" data-name="$($f.Name)">
    <div class="preview"><div class="preview-inner">$rawTrim</div></div>
    <div class="meta">
      <div class="name">$($f.Name)</div>
      <div class="import">import x from '${importPath}?raw';</div>
      <button type="button" class="copy-btn" data-copy-target="src-$($f.Name)">Copy SVG</button>
    </div>
    <pre class="src" id="src-$($f.Name)"><code>$escaped</code></pre>
  </article>
"@
        [void]$sections.AppendLine($card)
    }
    [void]$sections.AppendLine("</div>")
    [void]$sections.AppendLine("</section>")
}

$totalCount = $files.Count

$html = @"
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>omni-sheet-vault - D&amp;D 5e icon index</title>
<style>
  :root {
    color-scheme: light;
    --bg: #F9F9F9;
    --panel: #FFFFFF;
    --border: #D8D8D8;
    --border-strong: #BFCCD6;
    --text: #242528;
    --text-muted: #6B7A85;
    --accent: #92A2B3;
    --ink: #4A5D6B;
    --code-bg: #F3F4F5;
  }
  * { box-sizing: border-box; }
  body {
    margin: 0;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    background: var(--bg);
    color: var(--text);
  }
  header {
    position: sticky;
    top: 0;
    z-index: 5;
    background: var(--ink);
    color: #fff;
    padding: 16px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }
  header h1 { font-size: 16px; margin: 0; font-weight: 700; }
  header .count { font-size: 12px; color: #C7D2D9; }
  header input {
    flex: 0 1 320px;
    padding: 8px 12px;
    border-radius: 4px;
    border: 1px solid var(--accent);
    font-size: 13px;
    background: #fff;
  }
  main { padding: 24px; max-width: 1400px; margin: 0 auto; }
  section.cat { margin-bottom: 40px; }
  section.cat h2 {
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: .05em;
    color: var(--accent);
    border-bottom: 1px solid var(--border);
    padding-bottom: 8px;
    margin: 0 0 16px;
  }
  .grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
  }
  .card {
    background: var(--panel);
    border: 1px solid var(--border);
    border-radius: 6px;
    padding: 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .preview {
    height: 88px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: repeating-conic-gradient(#eef0f2 0% 25%, #fafbfc 0% 50%) 0 0 / 16px 16px;
    border-radius: 4px;
    color: var(--ink);
  }
  .preview-inner { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; }
  .preview-inner svg { width: 100%; height: 100%; display: block; }
  .meta { display: flex; flex-direction: column; gap: 4px; }
  .name { font-size: 12px; font-weight: 700; word-break: break-all; }
  .import {
    font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
    font-size: 10px;
    color: var(--text-muted);
    word-break: break-all;
  }
  .copy-btn {
    align-self: flex-start;
    font-size: 11px;
    border: 1px solid var(--border-strong);
    background: #fff;
    border-radius: 4px;
    padding: 4px 8px;
    cursor: pointer;
    color: var(--text);
  }
  .copy-btn:hover { background: var(--accent); color: #fff; border-color: var(--accent); }
  .copy-btn.copied { background: var(--ink); color: #fff; border-color: var(--ink); }
  pre.src {
    display: none;
    margin: 0;
    background: var(--code-bg);
    border: 1px solid var(--border);
    border-radius: 4px;
    padding: 8px;
    font-size: 10px;
    line-height: 1.4;
    overflow-x: auto;
    max-height: 160px;
  }
  pre.src.open { display: block; }
  .card.hidden { display: none; }
  section.cat.empty { display: none; }
  footer { text-align: center; padding: 24px; font-size: 11px; color: var(--text-muted); }
</style>
</head>
<body>
<header>
  <h1>D&amp;D 5e icon index</h1>
  <span class="count">$totalCount SVGs - apps/web/src/systems/dnd5e/frames/</span>
  <input type="search" id="filter" placeholder="Filter by filename...">
</header>
<main>
$($sections.ToString())
</main>
<footer>Generated from apps/web/src/systems/dnd5e/frames/*.svg. Regenerate after adding or renaming an icon.</footer>
<script>
document.querySelectorAll('.card').forEach(function (card) {
  var btn = card.querySelector('.copy-btn');
  var pre = card.querySelector('pre.src');
  btn.addEventListener('click', function () {
    var text = pre.textContent;
    navigator.clipboard.writeText(text).then(function () {
      btn.textContent = 'Copied!';
      btn.classList.add('copied');
      setTimeout(function () { btn.textContent = 'Copy SVG'; btn.classList.remove('copied'); }, 1200);
    });
  });
  card.querySelector('.preview').addEventListener('click', function () {
    pre.classList.toggle('open');
  });
});

var filterInput = document.getElementById('filter');
filterInput.addEventListener('input', function () {
  var q = filterInput.value.trim().toLowerCase();
  document.querySelectorAll('.card').forEach(function (card) {
    var name = card.getAttribute('data-name').toLowerCase();
    card.classList.toggle('hidden', q.length > 0 && name.indexOf(q) === -1);
  });
  document.querySelectorAll('section.cat').forEach(function (sec) {
    var visible = sec.querySelectorAll('.card:not(.hidden)').length;
    sec.classList.toggle('empty', visible === 0);
  });
});
</script>
</body>
</html>
"@

Set-Content -Path $outFile -Value $html -Encoding utf8
Write-Output "Wrote $outFile"
Write-Output "Total icons: $totalCount"
