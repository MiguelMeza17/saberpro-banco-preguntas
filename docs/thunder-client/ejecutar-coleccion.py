"""Ejecuta en orden la colección de Thunder Client y comprueba el código HTTP esperado de cada request.

Uso (con el stack arriba y recién creado: docker compose down -v && docker compose up -d --build):
    python3 docs/thunder-client/ejecutar-coleccion.py
Solo usa la biblioteca estándar.
"""
import json
import pathlib
import sys
import time
import urllib.error
import urllib.request

COLECCION = pathlib.Path(__file__).with_name("thunder-collection_saberpro.json")


def ejecutar(req):
    datos = req["body"]["raw"].encode("utf-8") if "body" in req else None
    peticion = urllib.request.Request(req["url"], data=datos, method=req["method"])
    for h in req.get("headers", []):
        peticion.add_header(h["name"], h["value"])
    try:
        with urllib.request.urlopen(peticion, timeout=15) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")


def main():
    coleccion = json.loads(COLECCION.read_text(encoding="utf-8"))
    fallos = 0
    for req in sorted(coleccion["requests"], key=lambda r: r["sortNum"]):
        esperado = int(req["tests"][0]["value"])
        codigo, cuerpo = ejecutar(req)
        ok = codigo == esperado
        fallos += not ok
        print(f"{'OK ' if ok else 'ERR'} {codigo} (esperado {esperado})  {req['method']:4} {req['url']}  — {req['name']}")
        if not ok:
            print("    ", cuerpo[:300])
        if "enviar-revision" in req["url"]:
            time.sleep(1.5)  # deja que lifecycle-service consuma el mensaje
    print(f"\n{len(coleccion['requests']) - fallos}/{len(coleccion['requests'])} requests con el código esperado")
    sys.exit(1 if fallos else 0)


if __name__ == "__main__":
    main()
