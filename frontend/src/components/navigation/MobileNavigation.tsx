"use client";

import Image from "next/image";
import { useCallback, useEffect, useId, useRef, useState } from "react";

import { Button, TextLink } from "@/components/ui";
import { classNames } from "@/components/ui/classNames";

import { PUBLIC_NAVIGATION_ITEMS } from "./navigation";
import { PrimaryNavigation } from "./PrimaryNavigation";
import styles from "./PublicNavigation.module.css";

const FOCUSABLE_SELECTOR = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])',
].join(",");

export interface MobileNavigationProps {
  className?: string;
}

export function MobileNavigation({ className }: MobileNavigationProps) {
  const generatedId = useId().replace(/:/g, "");
  const dialogId = `mobile-navigation-${generatedId}`;
  const titleId = `${dialogId}-title`;
  const triggerId = `${dialogId}-trigger`;
  const dialogRef = useRef<HTMLDialogElement>(null);
  const drawerRef = useRef<HTMLDivElement>(null);
  const previousBodyOverflow = useRef("");
  const [open, setOpen] = useState(false);

  const requestClose = useCallback(() => {
    setOpen(false);
  }, []);

  const returnFocus = useCallback(() => {
    window.requestAnimationFrame(() => {
      document.getElementById(triggerId)?.focus();
    });
  }, [triggerId]);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) {
      return;
    }

    if (open && !dialog.open) {
      previousBodyOverflow.current = document.body.style.overflow;
      document.body.style.overflow = "hidden";
      dialog.showModal();
      window.requestAnimationFrame(() => {
        drawerRef.current
          ?.querySelector<HTMLElement>(FOCUSABLE_SELECTOR)
          ?.focus();
      });
      return;
    }

    if (!open && dialog.open) {
      dialog.close();
    }
  }, [open]);

  useEffect(() => {
    return () => {
      document.body.style.overflow = previousBodyOverflow.current;
    };
  }, []);

  const handleDialogClosed = () => {
    document.body.style.overflow = previousBodyOverflow.current;
    if (open) {
      setOpen(false);
    }
    returnFocus();
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (event.key === "Escape") {
      event.preventDefault();
      requestClose();
      return;
    }

    if (event.key !== "Tab") {
      return;
    }

    const focusable = Array.from(
      drawerRef.current?.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR) ?? [],
    ).filter((element) => !element.hasAttribute("disabled"));

    if (!focusable.length) {
      event.preventDefault();
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  };

  return (
    <div className={classNames(styles.mobileRoot, className)}>
      <Button
        aria-controls={dialogId}
        aria-expanded={open}
        className={styles.menuTrigger}
        icon={
          <Image
            alt=""
            className={styles.menuIcon}
            height={20}
            src="/icons/yegov-menu.svg"
            width={20}
          />
        }
        id={triggerId}
        onClick={() => setOpen(true)}
        variant="ghost"
      >
        القائمة
      </Button>

      <dialog
        aria-labelledby={titleId}
        className={styles.dialog}
        id={dialogId}
        onCancel={(event) => {
          event.preventDefault();
          requestClose();
        }}
        onClose={handleDialogClosed}
        onMouseDown={(event) => {
          if (event.target === event.currentTarget) {
            requestClose();
          }
        }}
        ref={dialogRef}
      >
        <div className={styles.drawer} onKeyDown={handleKeyDown} ref={drawerRef}>
          <header className={styles.drawerHeader}>
            <h2 className={styles.drawerTitle} id={titleId}>
              القائمة الرئيسية
            </h2>
            <Button onClick={requestClose} variant="ghost">
              إغلاق
            </Button>
          </header>

          <PrimaryNavigation
            className={styles.drawerNavigation}
            items={PUBLIC_NAVIGATION_ITEMS}
            label="التنقل الرئيسي للجوال"
            onNavigate={requestClose}
            variant="drawer"
          />

          <footer className={styles.drawerFooter}>
            <TextLink href="/contact" onClick={requestClose}>
              تواصل معنا
            </TextLink>
            <p className={styles.drawerNote}>الجمهورية اليمنية · بوابة حكومية رقمية</p>
          </footer>
        </div>
      </dialog>
    </div>
  );
}
