# CLAUDE.md

## Knowledge Base

Architecture, conventions, domain model and API contracts for AutoPay live in the
`autopay-knowledgebase` repo, normally cloned alongside this one. Locate it by checking, in order,
`$AUTOPAY_KB`, then `../autopay-knowledgebase`, then each parent directory. **A candidate only counts
if it contains a readable `CLAUDE.shared.md`** — skip any that does not, including a set but stale
`$AUTOPAY_KB`. Read that file **before searching the KB** and follow its routing and reading rules: the
routing table names the right page directly, and grepping blind is slower and pulls in heavy docs you
don't need. If no candidate has it, say the KB is unavailable rather than answering from guesswork.

Consult it before planning, implementing, debugging or reviewing, and check it for prior art before
inventing a new pattern.

This repo is a **public, MIT-licensed library on Java 11 with zero compile dependencies** — not a
deployed service, and a deliberate exception to the Java 21 baseline. Treat anything written here as
externally visible, and do not add a compile dependency: both are breaking changes for consumers. Its
package layout, the builder-based signature/verification spec pattern, the structured-field type
hierarchy, the ECDSA P1363 gotcha, consumer pins and the release-to-Central flow are documented in the
KB at `03-products/01-autopay/09-codebases/15-http-signatures.md` — read
`15-http-signatures-summary.md` first. Not duplicated here; update the KB doc rather than re-adding it
to this file.

Before opening a PR, check whether the change needs a KB update rather than deciding by hand — apply
the gate in the KB's `0-meta/durability-gate.md`. In Claude Code the optional `/kb-check` skill
automates exactly that; installing it is not required.
