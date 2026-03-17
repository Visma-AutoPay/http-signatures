# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java library implementing the IETF HTTP Message Signatures specification (RFC 9421), along with Digest Fields (RFC 9530) and Structured Field Values for HTTP (RFC 8941). The library provides high-level interfaces for creating and verifying HTTP signatures for end-to-end integrity and authenticity.

**Requirements:** Java 11 or newer. No compile dependencies.

## Architecture

### Package Structure

The codebase is organized into three main functional areas:

1. **`net.visma.autopay.http.signature`** - HTTP Message Signatures (RFC 9421)
2. **`net.visma.autopay.http.digest`** - Digest Fields (RFC 9530)
3. **`net.visma.autopay.http.structured`** - Structured Field Values (RFC 8941)

### Signature Package Architecture

The signature package follows a builder-based specification pattern with clear separation between:

**Specification Objects (immutable):**
- `SignatureSpec` - Complete specification for creating a signature (components, parameters, context, private key, label)
- `VerificationSpec` - Complete specification for verifying a signature (required/forbidden parameters, required components, context, public key getter, label/tag, time limits)
- `SignatureComponents` - Defines which HTTP message parts to include (headers, derived components like @method, @path, @authority)
- `SignatureParameters` - Metadata for signatures (created, expires, nonce, keyid, algorithm, tag)
- `SignatureContext` - Container for actual HTTP message values (method, URI, status, headers, trailers, related request context)

**Core Operations:**
- `SignatureSigner` - Low-level signing operations
- `SignatureVerifier` - Low-level verification operations
- `DataSigner` / `DataVerifier` - Handle cryptographic operations using Java security providers

**Supporting Classes:**
- `Component` / `HeaderComponent` / `DerivedComponent` - Individual signature component representations
- `SignatureAlgorithm` / `SignatureKeyAlgorithm` - Algorithm definitions (RSA, ECDSA, EdDSA, HMAC)
- `PublicKeyInfo` - Encapsulates public key with algorithm information
- `SignatureResult` - Output containing signature headers and base string

### Structured Fields Architecture

Structured fields implement RFC 8941 with a type hierarchy:

**Base Types:**
- `StructuredItem` - Abstract base for all items (has optional parameters)
- `StructuredField` - Marker interface for top-level fields (Lists and Dictionaries)

**Concrete Item Types:**
- `StructuredInteger` (stores `long`)
- `StructuredDecimal` (stores `BigDecimal`)
- `StructuredString` (stores `String`)
- `StructuredToken` (stores `String`)
- `StructuredBytes` (stores `byte[]`)
- `StructuredBoolean` (stores `boolean`)

**Collection Types:**
- `StructuredList` - Top-level list (List&lt;StructuredItem&gt;)
- `StructuredInnerList` - Nested list within a list or dictionary
- `StructuredDictionary` - Top-level dictionary (LinkedHashMap&lt;String, StructuredItem&gt;)
- `StructuredParameters` - Parameters attached to items (LinkedHashMap&lt;String, StructuredItem&gt;)

**Utilities:**
- `StructuredParser` - Parses RFC 8941 serialized strings into structured objects
- `CharacterValidator` - Validates characters according to RFC 8941 rules

### Digest Package

Simple utility classes for computing and verifying Content-Digest headers:
- `DigestCalculator` - Computes SHA-256 or SHA-512 digests of request/response bodies
- `DigestVerifier` - Verifies digest headers against actual content
- `DigestAlgorithm` - Enum of supported algorithms (SHA_256, SHA_512)

## Key Design Patterns

### Security Provider Abstraction
The library uses Java's default security providers but allows third-party providers (like Bouncy Castle) to be registered. For ECDSA signatures, the library specifically uses P1363 format algorithms (e.g., `SHA256withECDSAinP1363Format`) as required by RFC 9421.

### Exception Handling
- `SignatureException` - Thrown for all signature creation/verification failures
- `DigestException` - Thrown for digest computation/verification failures
- `StructuredException` - Thrown for structured field parsing/serialization failures

All exceptions extend from `Exception` (checked exceptions), requiring explicit handling.

## Testing

- Test utilities include `ObjectMother` for test data generation
- Specification tests in `net.visma.autopay.http.structured.spec` use JSON test vectors from RFC 8941
- Bouncy Castle provider is available in test scope for testing with third-party security providers

## Common Development Scenarios

### Adding Support for New Signature Algorithms
1. Add algorithm to `SignatureAlgorithm` enum
2. Update `SignatureKeyAlgorithm` to map key algorithm
3. Add tests in `DataSignerTest` and `DataVerifierTest`
4. Consider provider compatibility (especially for ECDSA P1363 format)

### Adding New Derived Components
1. Add component type to `DerivedComponentType` enum
2. Update `ComponentFactory` to handle extraction from `SignatureContext`
3. Add builder methods to `SignatureComponents.Builder`
4. Add tests covering serialization and extraction

### Modifying Structured Field Parsing
Changes to `StructuredParser` should maintain RFC 8941 compliance. Run the specification tests in `StructuredSpecificationTest` which use official test vectors.
